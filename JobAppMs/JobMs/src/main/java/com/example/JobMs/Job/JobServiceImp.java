package com.example.JobMs.Job;


import com.example.JobMs.Clients.CompanyClient;
import com.example.JobMs.Clients.ReviewClient;
import com.example.JobMs.Configuration.AppConfig;
import com.example.JobMs.DTO.Company;
import com.example.JobMs.DTO.JobDto;
import com.example.JobMs.DTO.Review;
import com.example.JobMs.Mapper.JobMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobServiceImp implements JobService{


    JobRepository jobRepository;

    private final CompanyClient companyClient;

    private final ReviewClient reviewClient;

    public JobServiceImp(JobRepository jobRepository, CompanyClient companyClient,ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.reviewClient = reviewClient;
    }


    @Override
    public List<JobDto> findAll() {

        List<Job> jobs = jobRepository.findAll();
        List<JobDto> jobDtos = new ArrayList<>();

        return jobs.stream().map(this::convertJobDto).collect(Collectors.toList());
    }

    @Override
    public Boolean createJob(Job job) {
        Company company;
        try {
            company = companyClient.getCompany(job.getCompanyId());
            jobRepository.save(job);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public JobDto findJobById(Long id) {

        Job job = jobRepository.findById(id).orElse(null);
        if(job != null)
        {
            return convertJobDto(job);
        }

        return null;
    }

    @Override
    public Boolean removeJobById(Long id) {
       if(jobRepository.existsById(id)){
           jobRepository.deleteById(id);
           return true;
       }
       else{
           return false;
       }
    }

    @Override
    public Boolean updateJobById(Long id, Job job) {
        Optional<Job> optionalJob = jobRepository.findById(id);

            if(optionalJob.isPresent())
            {
                Job jobToUpdate = optionalJob.get();
                jobToUpdate.setDescription(job.getDescription());
                jobToUpdate.setTitle(job.getTitle());
                jobToUpdate.setMinSalary(job.getMinSalary());
                jobToUpdate.setMaxSalary(job.getMaxSalary());
                jobToUpdate.setLocation(job.getLocation());
                jobRepository.save(jobToUpdate);
                return true;
            }
        return false;
    }

    private JobDto convertJobDto(Job job)
    {
        JobDto jobDto;

        Company company = companyClient.getCompany(job.getCompanyId());

        List<Review> reviews = reviewClient.getReviews(job.getCompanyId());

        if (company != null) {
            jobDto = JobMapper.getJobDto(job,company,reviews);
            return jobDto;
        }

        return null;
    }
}
